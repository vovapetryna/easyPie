import AuthForm from "./components/AuthForm";
import FileList from "./components/FileList";
import Input from "./components/Input";
import Log from "./components/Log";

function App() {
  return (
    <div className="container">
      <div className="row">
        <div className="col">
          <AuthForm/>
        </div>
      </div>
      <div className="row">
        <div className="col">
          <FileList />
        </div>
        <div className="col">
          <Input/>
        </div>
        <div className="col">
          <Log/>
        </div>
      </div>
    </div>
  );
}

export default App;
