import React from "react";
import {connect} from "react-redux";
import {startEditSession} from "../redux/fileReducers";
import "../styles/main.css";

const File = (props) => {
  return (
    <div className="Smr-1 line Smb-1">
      <button
        type="button"
        className="btn btn-outline-secondary"
        onClick={_ => props.startEditSession(props.file)}>{props.file}
      </button>
    </div>
  )
}

export default connect(null,
  {
    startEditSession
  })(File);