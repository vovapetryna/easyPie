import React from "react";
import {connect} from "react-redux";

const Input = (props) => {

  if (props.file !== undefined) {
    return (
      <div className="card">
        <div className="card-header">File: {props.file}</div>
        <div className="card-body">
          <div className="mb-3">
            <label htmlFor="textarea" className="form-label">use `text` for italic</label>
            <textarea className="form-control" id="textarea" rows="10"/>
          </div>
        </div>
      </div>
    )
  } else {
    return (
      <div className="card">
        <div className="card-header">Open or create file</div>
      </div>
    )
  }

}

export default connect(
  state => ({
    file: state.files.currentFile
  }),
  null)(Input);
