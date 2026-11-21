let accessToken = null;
let lastId = null;
let deadline = 0;
let timer = null;

let generation = 0;
let busy = false;

const el = id => document.getElementById(id);

const selected = () =>
    el('rentalId').value.trim() || lastId || '';

const messages = {
    200: 'Request successful',
    201: 'Rental created successfully',
    400: 'Check your input',
    401: 'Authentication required',
    403: 'Access denied for your role',
    404: 'Rental not found',
    409: 'Operation conflicts with a business rule'
};


// 1. Display API response

function display(status, data, note) {

    el('httpStatus').textContent =
        status ? `HTTP ${status}` : 'Network error';

    el('notice').textContent =
        note || messages[status] || 'Request failed';

    el('notice').className =
        status >= 200 && status < 300
            ? 'success'
            : 'error';

    const safe = Object.fromEntries(
        Object.entries(data || {})
            .filter(([k]) => k !== 'accessToken')
    );

    el('result').textContent =
        JSON.stringify(safe, null, 2);
}


// 2. Secure API requests

async function api(path, options = {}, withToken = true) {

    const headers = {
        'X-Correlation-Id': 'web-' + crypto.randomUUID(),

        ...(options.body
            ? {'Content-Type': 'application/json'}
            : {})
    };

    if (withToken && accessToken) {
        headers.Authorization = 'Bearer ' + accessToken;
    }

    try {

        const response = await fetch(path, {
            ...options,
            headers
        });

        const data = await response.json()
            .catch(() => ({}));

        return {
            status: response.status,
            data
        };

    } catch {

        return {
            status: 0,
            data: {
                message: 'Cannot reach Spring Boot server'
            }
        };
    }
}


// 3. Activity history

function history(action, status) {

    const li = document.createElement('li');

    li.textContent =
        `${new Date().toLocaleTimeString()} · ${action} · HTTP ${status}`;

    el('history').prepend(li);

    if (el('history').children.length > 20) {
        el('history').lastChild.remove();
    }
}


// 4. Rental status indicator

function renderRental(data = {}) {

    const status = data.status || 'UNKNOWN';

    el('state').textContent = status;
    el('state').dataset.status = status;

    el('item').textContent =
        data.title || 'No rental selected';

    const order = [
        'BOOKED',
        'RENTED',
        'RETURNED'
    ];

    document.querySelectorAll('[data-step]')
        .forEach(node => {

            node.classList.toggle(
                'active',
                order.indexOf(node.dataset.step)
                <= order.indexOf(status)
                && order.includes(status)
            );
        });
}


// 5. Logout

function logout(note = 'Signed out') {

    generation++;

    accessToken = null;

    clearInterval(timer);

    deadline = 0;

    el('sessionPane').hidden = true;
    el('loginPane').hidden = false;

    el('password').value = '';
    el('who').textContent = '';
    el('role').textContent = '';
    el('expiry').textContent = '';

    el('history').replaceChildren();

    renderRental();

    el('notice').textContent = note;
    el('httpStatus').textContent = 'Ready';
    el('result').textContent = note;
}


// 6. JWT countdown

function tick() {

    const seconds = Math.max(
        0,
        Math.ceil((deadline - Date.now()) / 1000)
    );

    const minutes = Math.floor(seconds / 60);

    const remainingSeconds =
        String(seconds % 60).padStart(2, '0');

    el('expiry').textContent =
        `Session remaining: ${minutes}:${remainingSeconds}`;

    if (!seconds) {
        logout('Session expired. Sign in again.');
    }
}


// 7. Prevent duplicate requests

async function locked(fn) {

    if (busy) return;

    busy = true;

    try {
        await fn(generation);
    } finally {
        busy = false;
    }
}


// 8. JWT Login

el('loginForm').addEventListener('submit', event => {

    event.preventDefault();

    locked(async g => {

        const login = await api(
            '/api/v1/auth/login',
            {
                method: 'POST',
                body: JSON.stringify({
                    username: el('username').value.trim(),
                    password: el('password').value
                })
            },
            false
        );

        if (g !== generation) return;

        display(login.status, login.data);

        if (
            login.status !== 200 ||
            !login.data.accessToken
        ) {
            return;
        }

        accessToken = login.data.accessToken;

        const me = await api('/api/me');

        if (g !== generation) return;

        if (me.status !== 200) {

            logout();

            display(me.status, me.data);

            return;
        }

        const roles = me.data.authorities || [];

        const manager =
            roles.includes('ROLE_MANAGER');

        el('who').textContent = me.data.username;

        el('role').textContent = roles.join(', ');

        el('permission').textContent = manager
            ? 'You can complete a RENTED rental.'
            : 'STAFF cannot complete rentals. Use the button to demonstrate HTTP 403.';

        el('complete').textContent = manager
            ? 'Complete rental · MANAGER'
            : 'Test forbidden action · STAFF';

        el('loginPane').hidden = true;

        el('sessionPane').hidden = false;

        el('password').value = '';

        deadline =
            Date.now() +
            Number(login.data.expiresIn || 900) * 1000;

        clearInterval(timer);

        timer = setInterval(tick, 1000);

        tick();

        display(me.status, me.data);

        history('Sign in', 200);
    });
});


// 9. Rental operations

async function run(
    action,
    suffix = '',
    method = 'GET',
    body = null
) {

    await locked(async g => {

        const id = selected();

        if (action !== 'Create' && !id) {

            el('notice').textContent =
                'Enter a rental UUID first.';

            return;
        }

        const path = '/api/v1/rentals' +
            (
                action === 'Create'
                    ? ''
                    : '/' + encodeURIComponent(id) + suffix
            );

        const response = await api(path, {

            method,

            ...(body
                ? {body: JSON.stringify(body)}
                : {})
        });

        if (g !== generation) return;

        display(response.status, response.data);

        history(action, response.status);


        // Invalid or expired JWT

        if (response.status === 401) {

            logout('Session expired or invalid.');

            display(response.status, response.data);

            return;
        }


        // Successful operation

        if (
            response.status === 200 ||
            response.status === 201
        ) {

            if (response.data.id) {

                lastId = response.data.id;

                el('rentalId').value = lastId;
            }

            renderRental(response.data);
        }


        // Protected or invalid status transition

        else if (
            (
                response.status === 403 ||
                response.status === 409
            )
            && id
            && action !== 'Create'
        ) {

            const read = await api(
                '/api/v1/rentals/' +
                encodeURIComponent(id)
            );

            if (g !== generation) return;

            if (read.status === 200) {

                renderRental(read.data);

                display(response.status, {
                    ...response.data,
                    currentStatus: read.data.status
                });
            }
        }
    });
}


// 10. Create rental

el('create').onclick = () => run(

    'Create',

    '',

    'POST',

    {
        businessKey:
            el('businessKey').value.trim()
            || `RENT-${Date.now()}`,

        title:
            el('title').value.trim()
    }
);


// 11. Read rental status

el('read').onclick = () =>
    run('Read');


// 12. BOOKED -> RENTED

el('start').onclick = () =>
    run('Start', '/start', 'POST');


// 13. RENTED -> RETURNED

el('complete').onclick = () =>
    run('Complete', '/complete', 'POST');


// 14. Reset status when changing UUID

el('rentalId').addEventListener('input', () => {

    lastId = null;

    renderRental();
});


// 15. Logout button

el('logout').onclick = () =>
    logout();


// 16. Copy rental UUID

el('copy').onclick = async () => {

    if (!selected()) return;

    try {

        await navigator.clipboard.writeText(
            selected()
        );

        el('notice').textContent = 'UUID copied.';

    } catch {

        el('rentalId').select();
    }
};


// 17. Test API without JWT token

el('anonymous').onclick = () => locked(async g => {

    const response = await api(

        '/api/v1/rentals/' +

        encodeURIComponent(
            selected() || crypto.randomUUID()
        ),

        {},

        false
    );

    if (g !== generation) return;

    display(response.status, response.data);

    if (accessToken) {

        history(
            'Anonymous access test',
            response.status
        );
    }
});