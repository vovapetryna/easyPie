import React from 'react';
import {render} from 'react-dom';
import App from './App';
import {applyMiddleware, compose, createStore} from "redux";
import {rootReducer} from "./redux/reducer";
import {Provider} from "react-redux";
import thunk from "redux-thunk";

export const store = createStore(rootReducer, compose(applyMiddleware(thunk)));

render(
  <React.StrictMode>
    <Provider store={store}><App/></Provider>
  </React.StrictMode>,
  document.getElementById('root')
);
