export function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

export function httpRequest(path, body) {
    return fetch(path, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body)
    });
}

export function decodeJWT(token) {
    let base64Url = token.split('.')[1];
    let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    let jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

export function hashCode(s) {
    var h = 0, l = s.length, i = 0;
    if (l > 0)
        while (i < l)
            h = (h << 5) - h + s.charCodeAt(i++) | 0;
    return h;
}

export function displayDocList(id) {
    return ` 
    <a href="/files/index.html?documentId=${id.$oid}" 
    style="display: flex; color: #313131; font-weight: 100; font-size: 1.1em; text-decoration: none; margin-bottom: 0.5em;">
        <img src="/files/file.svg" alt="" class="icon" style="margin-right: 0.5em"/>
        ${id.$oid}
        <img src="/files/share.svg" alt="" class="icon" style="margin-left: auto;"/>
    </a> `;
}

export function Reload() {
    return {
        Reload: {}
    };
}

export function Action(value) {
    return {
        Action: {
            action: value
        }
    };
}