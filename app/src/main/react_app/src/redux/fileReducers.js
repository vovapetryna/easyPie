const ADD_FILE = "ADD_FILE"
const SET_FILES = "SET_FILES"
const SET_ACTIVE_FILE = "SET_ACTIVE_FILE"
const SET_JOIN_ID = "SET_JOIN_ID"

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

export const setActiveFile = (file) => ({
  type: SET_ACTIVE_FILE,
  payload: file
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
  joinId: undefined
};

export const fileReducer = (state = initialState, action) => {
  switch (action.type) {
    case ADD_FILE:
      return {...state, files: [...state.files, action.payload]}
    case SET_FILES:
      return {...state, files: action.payload}
    case SET_ACTIVE_FILE:
      return {...state, currentFile: action.payload}
    case SET_JOIN_ID:
      return {...state, joinId: action.payload}
    default:
      return state
  }
}