import React, {useEffect} from "react";
import File from "./File";
import {connect} from "react-redux";
import {addFile, fileCreate, fileReload} from "../redux/fileReducers";
import JoinFile from "./JoinFile";
import "../styles/main.css";

const FilesList = (props) => {
  useEffect(() => {
    if (props.session._id !== undefined)
      props.fileReload();
  }, [props.session])

  return (
    <div className="card">
      <div className="card-header">
        <div className="btn-toolbar justify-content-between">
          Files
          <div>
            <button
              className="btn btn-primary Smr-1"
              onClick={_ => props.fileCreate()}>Create file
            </button>
            <button
              className="btn btn-primary"
              onClick={_ => props.fileReload()}>Reload
            </button>
          </div>
        </div>
      </div>
      <div className="card-body">
        <JoinFile/>
        <div>{props.files.map(file => <File file={file} key={file}/>)}</div>
      </div>
    </div>
  )
};

const mapStateToProps = state => ({
  files: state.files.files,
  session: state.auth.session
});

export default connect(
  mapStateToProps,
  {
    addFile,
    fileReload,
    fileCreate
  })(FilesList);