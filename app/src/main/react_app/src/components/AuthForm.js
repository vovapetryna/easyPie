import React from "react";
import {connect} from "react-redux";
import {getSession, getToken, logout, setLogin, setSession, setToken} from "../redux/authReducers";

const Auth = (props) => {
  const submitHandler = (event) => {
    event.preventDefault();
    props.setLogin("");
    props.setToken("");
    props.getSession({login: props.login, token: props.token});
  };

  if (props.session._id === undefined) {
    return (
      <div className="card mb-3 mt-3">
        <div className="card-header">Session actions</div>
        <div className="card-body">
          <form onSubmit={submitHandler}>
            <div className="mb-3">
              <input
                type="text"
                className="form-control"
                id="loginInput"
                placeholder="login"
                value={props.login}
                onChange={e => props.setLogin(e.target.value)}
              />
            </div>
            <div className="input-group mb-3">
              <input
                type="password"
                className="form-control"
                id="tokenInput"
                placeholder="Token"
                value={props.token}
                onChange={e => props.setToken(e.target.value)}/>
              <button
                className="btn btn-outline-secondary"
                type="button"
                id="getTokenBtn"
                onClick={_ => props.getToken(props.login)}>Get token
              </button>
            </div>
            <button type="submit" className="btn btn-primary">Authenticate</button>
          </form>
        </div>
      </div>
    )
  } else {
    return (
      <div className="card mb-3 mt-3">
        <div className="card-header">Session actions</div>
        <div className="card-body">
          <button
            className="btn btn-primary"
            onClick={_ => props.logout()}>Logout
          </button>
        </div>
      </div>
    )
  }


}

const mapStateToProps = state => ({
  session: state.auth.session,
  login: state.auth.login,
  token: state.auth.token
});


export default connect(
  mapStateToProps,
  {
    getSession,
    setSession,
    getToken,
    setLogin,
    setToken,
    logout
  })(Auth);

