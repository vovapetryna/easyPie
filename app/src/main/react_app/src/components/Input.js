import React from "react";
import {connect} from "react-redux";
import {client} from "scala_bundle";
import {hashCode} from "../textUtils/hash";
import {setCaretPos} from "../redux/fileReducers";

const Input = (props) => {

  if (props.file !== undefined) {
    return (
      <div className="card mt-3">
        <div className="card-header">File: {props.file}</div>
        <div className="card-body">
          <div className="mb-3">
            <label htmlFor="textarea" className="form-label">use `text` for italic</label>
            <textarea
              className="form-control"
              id="textarea"
              rows="10"
              value={props.state.value}
              onChange={e => {
                const sid = hashCode(props.session._id.$oid);
                props.setCaretPos(e.target.selectionStart);
                client.nextText(e.target.value, sid, props.client);
              }}
            />
          </div>
        </div>
      </div>
    )
  } else {
    return (
      <div className="card mt-3">
        <div className="card-header">Open or create file</div>
      </div>
    )
  }

}

export default connect(
  state => ({
    file: state.files.currentFile,
    session: state.auth.session,
    client: state.files.client,
    state: state.files.state
  }),
  {
    setCaretPos
  })(Input);
