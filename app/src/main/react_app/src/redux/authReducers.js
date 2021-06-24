import {closeConnection} from "./fileReducers";

const SET_SESSION = "SET_SESSION";
const SET_LOGIN = "SET_LOGIN";
const SET_TOKEN = "SET_TOKEN";

export const setLogin = (login) => ({
  type: SET_LOGIN,
  payload: login
});

export const setToken = (token) => ({
  type: SET_TOKEN,
  payload: token
});


function getCookie(name) {
  let matches = document.cookie.match(new RegExp(
    "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
  ));
  return matches ? decodeURIComponent(matches[1]) : undefined;
}

export function decodeJWT(token) {
  let base64Url = token.split('.')[1];
  let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  let jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
  }).join(''));
  return JSON.parse(jsonPayload);
}

export const setSession = _ => {
  const cookieSession = getCookie("auth")
  return {
    type: SET_SESSION,
    payload: cookieSession ? decodeJWT(cookieSession).auth : {}
  }
};


export const getSession = (loginToken) => {
  return async dispatch => {
    await fetch("/auth/login", {
      method: "POST",
      body: JSON.stringify(loginToken)
    });
    dispatch(setSession());
  }
}

export const getToken = (login) => {
  return async dispatch => {
    const response = await fetch("/auth/register", {
      method: "POST",
      body: JSON.stringify({login: login})
    });
    const body = await response.json()
    dispatch(setToken(body));
  }
}

export const logout = _ => {
  return async dispatch => {
    await fetch("/auth/logout")
    dispatch(setSession());
    dispatch(closeConnection);
  }
}

const initialState = _ => {
  const cookieSession = getCookie("auth")
  return {
    session: cookieSession ? decodeJWT(cookieSession).auth : {},
    login: "",
    token: ""
  }
};

export const authReducers = (state = initialState(), action) => {
  switch (action.type) {
    case SET_LOGIN:
      return {...state, login: action.payload};
    case SET_TOKEN:
      return {...state, token: action.payload}
    case SET_SESSION:
      return {...state, session: action.payload};
    default:
      return state;
  }
}