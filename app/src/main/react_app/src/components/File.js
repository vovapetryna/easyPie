import React from "react";
import {connect} from "react-redux";
import {setActiveFile} from "../redux/fileReducers";

const File = (props) => {
  return (
    <button
      type="button"
      className="btn btn-outline-secondary"
      onClick={_ => props.setActiveFile(props.file)}>{props.file}
    </button>
  )
}

export default connect(null,
  {
    setActiveFile
  })(File);