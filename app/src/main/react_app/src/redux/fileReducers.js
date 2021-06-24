import {client, client as Client, state} from "scala_bundle";

const ADD_FILE = "ADD_FILE"
const SET_FILES = "SET_FILES"
const SET_JOIN_ID = "SET_JOIN_ID"
const SET_CARET_POS = "SET_CARET_POS"
const RESET_CARET_POS = "RESET_CARET_POS"

const SET_ACTIVE_FILE = "SET_ACTIVE_FILE"
const SET_STATE = "SET_STATE"
const CLOSE_CONNECTION = "CLOSE_CONNECTION"

export const closeConnection = ({
  type: CLOSE_CONNECTION
})

export const resetCaretPos = ({
  type: RESET_CARET_POS
})

export const setCaretPos = (pos) => ({
  type: SET_CARET_POS,
  payload: pos
})

export const setState = (state) => ({
  type: SET_STATE,
  payload: state
})

export const joinToFile = (id) => {
  return async dispatch => {
    await fetch(`document/join/${id}`);
    dispatch(fileReload());
  }
}

export const setJoinId = (id) => ({
  type: SET_JOIN_ID,
  payload: id
});

export const startEditSession = (file) => {
  return async dispatch => {
    const client = await Client.create(e => {
      dispatch(setState(e));
      dispatch(resetCaretPos);
    });
    Client.start(
      `ws://localhost:9001/document/edit/${file}`,
      client
    );
    dispatch(closeConnection);
    dispatch(setActiveFile(file, client));
  }
}

export const setActiveFile = (file, client) => ({
  type: SET_ACTIVE_FILE,
  payload: {
    file: file,
    client: client
  }
});

export const setFiles = (files) => ({
  type: SET_FILES,
  payload: files
});

export const fileReload = _ => {
  return async dispatch => {
    const response = await fetch("document/list");
    const body = await response.json();
    dispatch(setFiles(body.map(f => f.$oid)));
  }
}

export const fileCreate = _ => {
  return async dispatch => {
    const response = await fetch("document/create");
    const body = await response.json();
    dispatch(addFile(body.$oid))
  }
}

export const addFile = (file) => ({
  type: ADD_FILE,
  payload: file
});

const initialState = {
  files: [],
  currentFile: undefined,
  joinId: undefined,
  client: undefined,
  state: state.empty,
  caretPos: 0
};

export const fileReducer = (state = initialState, action) => {
  switch (action.type) {
    case ADD_FILE:
      return {...state, files: [...state.files, action.payload]}
    case SET_FILES:
      return {...state, files: action.payload}
    case SET_ACTIVE_FILE:
      return {
        ...state,
        currentFile: action.payload.file,
        client: action.payload.client
      }
    case SET_JOIN_ID:
      return {...state, joinId: action.payload}
    case SET_STATE:
      return {...state, state: action.payload}
    case SET_CARET_POS:
      return {...state, caretPos: action.payload}
    case RESET_CARET_POS: {
      const elem = document.getElementById("textarea");
      if (elem) {
        elem.focus();
        elem.setSelectionRange(state.caretPos, state.caretPos);
      }
      return state
    }
    case CLOSE_CONNECTION: {
      if (state.client) {
        client.close(state.client)
      }
      return state
    }
    default:
      return state
  }
}