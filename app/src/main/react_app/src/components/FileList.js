import React, {useEffect} from "react";
import File from "./File";
import {connect} from "react-redux";
import {addFile, fileCreate, fileReload} from "../redux/fileReducers";
import JoinFile from "./JoinFile";

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
          <button
            className="btn btn-primary"
            onClick={_ => props.fileReload()}>Reload
          </button>
        </div>
      </div>
      <div className="card-body">
        <div>{props.files.map(file => <File file={file} key={file}/>)}</div>
        <button
          className="btn btn-primary mt-2"
          onClick={_ => props.fileCreate()}>Create file
        </button>
        <JoinFile/>
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