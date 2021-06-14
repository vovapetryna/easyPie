import {combineReducers} from "redux";
import {authReducers} from "./authReducers";
import {fileReducer} from "./fileReducers";

export const rootReducer = combineReducers({
  auth: authReducers,
  files: fileReducer
});