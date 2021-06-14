import {connect} from "react-redux";
import React from "react";
import {joinToFile, setJoinId} from "../redux/fileReducers";

const JoinFile = (props) => {
  return (
    <form>
      <div className="input-group mb-3">
        <input
          type="text"
          className="form-control"
          id="joinInput"
          placeholder="document id"
          value={props.joinId}
          onChange={e => props.setJoinId(e.target.value)}/>
        <button
          className="btn btn-outline-secondary"
          type="button"
          id="joinBtn"
          onClick={_ => props.joinToFile(props.joinId)}>Join
        </button>
      </div>
    </form>
  )
}

export default connect(state => ({
  joinId: state.files.joinId
}), {
  setJoinId,
  joinToFile
})(JoinFile);