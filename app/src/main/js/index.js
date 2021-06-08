import {Action, decodeJWT, displayDocList, getCookie, hashCode, httpRequest, Reload} from "/files/utils.js";
import {ActionUtil, TreeDoc} from "/files/main.js";

let state = {
    edit: false,
    auth: false,
    control: false
}

//page proxies
let views = {
    edit: () => document.getElementById("edit-view"),
    auth: () => document.getElementById("auth-view"),
    control: () => document.getElementById("control-view"),
    authButton: () => document.getElementById("auth-button"),
    loginInput: () => document.getElementById("login"),
    tokenInput: () => document.getElementById("token"),
    addButton: () => document.getElementById("add-button"),
    documentList: () => document.getElementById("doc-container"),
    input: () => document.getElementById("input")
}

function setView(edit, auth, control) {
    state = {
        edit: edit,
        auth: auth,
        control: control
    }
    if (!state.edit) views.edit().style.display = "none"; else views.edit().style.display = "block";
    if (!state.auth) views.auth().style.display = "none"; else views.auth().style.display = "block";
    if (!state.control) views.control().style.display = "none"; else views.control().style.display = "block";
    reRender();
}

//root data model
class RootModel {
    params = () => new URLSearchParams(window.location.search);
    session = () => getCookie("auth");
    siteId = () => hashCode(decodeJWT(this.session()).auth._id.$oid);
    isAuthed = () => getCookie("auth") !== undefined;
    token = "";
    login = "";
    authButtonText = () => this.isAuthButton() ? "authenticate" : "get token";
    isAuthButton = () => this.token.length > 0;
    documentList = async () => await fetch("/document/list").then(async v => await v.json());
    activeDocument = "";
    socket = null;

    myTree = null;
    prevText = "";
    actualText = () => this.myTree.jsRepr.map(v => v.value).join("");
}

let rootModel = new RootModel();

//render step
function reRender() {
    if (state.auth) {
        views.authButton().innerText = rootModel.authButtonText();
        views.tokenInput().value = rootModel.token;
    }

    if (state.control) {
        rootModel.documentList().then(value => {
            views.documentList().innerHTML = value.map(id => displayDocList(id)).join('');
        });
    }
}

//event listeners
views.tokenInput().oninput = _ => {
    rootModel.token = views.tokenInput().value;
    reRender();
}
views.loginInput().oninput = _ => rootModel.login = views.loginInput().value;
views.authButton().onclick = async _ => {
    if (rootModel.isAuthButton())
        httpRequest("/auth/login", {login: rootModel.login, token: rootModel.token}).then(_ => router());
    else
        httpRequest("/auth/register", {login: rootModel.login}).then(r => r.json().then(token => {
            rootModel.token = token;
            reRender();
        }));
}
views.addButton().onclick = _ => fetch("/document/create").then(_ => reRender());

views.input().oninput = _ => {
    const validateInput = (oldText, text) => (text.length === oldText.length + 1);
    const textDiff = (oldText, text) => {
        for (let i = 0; i < oldText.length; i++)
            if (oldText[i] !== text[i]) return [i, text[i]];
        return [text.length - 1, text[text.length - 1]];
    };
    const updateTree = (value, position) => {
        const atoms = rootModel.myTree.jsRepr;
        const leftAtom = atoms[position];
        const rightAtom = atoms[position + 1];
        const addAction = rootModel.myTree.addAction(value, leftAtom, rightAtom);
        return [rootModel.myTree.process(addAction), addAction];
    };

    const isChangeValid = validateInput(rootModel.prevText, views.input().value);
    if (!isChangeValid || rootModel.socket === null) {
        views.input().value = rootModel.prevText;
    } else {
        const diff = textDiff(rootModel.prevText, views.input().value);
        const [newTree, action] = updateTree(diff[1], diff[0]);
        rootModel.myTree = newTree;
        views.input().value = rootModel.actualText();
        rootModel.prevText = rootModel.actualText();
        rootModel.socket.send(JSON.stringify(Action(JSON.parse(ActionUtil.toJsonString(action)))));
    }
}

const joinHandler = (fallBack, andThen) => {
    if (rootModel.params().get("joinId") !== null) {
        return fetch(`/document/join/${rootModel.params().get("joinId")}`).then(_ => {
            rootModel.activeDocument = rootModel.params().get("joinId");
            andThen();
        });
    } else return fallBack(andThen);
}

const documentIdHandler = (andThen) => {
    if (rootModel.params().get("documentId") === null) {
        return rootModel.documentList().then(value => {
            if (value.length > 0) {
                rootModel.activeDocument = value[0].$oid;
                andThen();
            } else {
                fetch("/document/create")
                    .then(response => response.json()
                        .then(id => {
                            rootModel.activeDocument = id.$oid;
                            andThen();
                        }));
            }
        });
    } else return new Promise(() => {
        rootModel.activeDocument = rootModel.params().get("documentId");
        andThen()
    });
}

function socketHandler() {
    reRender();
    rootModel.socket = new WebSocket(`ws://${location.hostname}:${location.port}/document/edit/${rootModel.activeDocument}`)
    rootModel.socket.addEventListener("open", (e) => {
        rootModel.socket.send(JSON.stringify(Reload()));
    });

    rootModel.socket.addEventListener('message', (e) => {
        const message = JSON.parse(e.data);
        console.log(message);

        if (message.Reload !== undefined) {
            const stringTree = JSON.stringify(message.Reload.value);
            rootModel.myTree = TreeDoc.loads(stringTree, rootModel.siteId());
            rootModel.prevText = rootModel.actualText();
            views.input().value = rootModel.actualText();
        }

        if (message.Action !== undefined) {
            rootModel.myTree = rootModel.myTree.processString(JSON.stringify(message.Action.action))
            rootModel.prevText = rootModel.actualText();
            views.input().value = rootModel.actualText();
        }

    });
}

function router() {
    if (!rootModel.isAuthed()) {
        setView(false, true, false);
    } else {
        joinHandler(documentIdHandler, socketHandler);
        setView(true, false, true);
    }
}

router();