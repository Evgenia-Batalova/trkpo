import { useState, useCallback } from "react";
import axios from 'axios';
import { debounce } from "lodash";
import "../styles/login.css";

const Registration = ({ navigate }) => {
  const [message, setMessage] = useState("");
  const [name, setName] = useState("");
  const [regLogin, setRegLogin] = useState("");
  const [regPass, setRegPass] = useState("");
  const [repRegPass, setRepRegPass] = useState("");

  const handleName = (e) => {
    const value = e.target.value.replace(/[^a-zа-я0-9]/gi, '');
    setName(value);
  };

  const handler = useCallback(debounce(checkLogin, 1500), []);

  const handleRegLogin = async (e) => {
    const value = e.target.value.replace(/[^a-z0-9]/gi, '');
    
    if (value.length >= 8) {
      handler(value);
    }

    setRegLogin(value);
  };

  async function checkLogin(login) {
    await axios.get(`http://localhost:8080/verifyLogin?login=${login}`).then(result => {
      if (result.data.state === "Error") {
        setMessage(result.data.message);
        showMessage('messageReg', 'regButton');
      } else {
        if (!result.data.data) {
          setMessage("Логин занят");
          showMessage('messageReg', 'regButton');
        }
      }
    });
  }

  const handleRegPass = (e) => {
    setRegPass(e.target.value);
  };

  const handleRepRegPass = (e) => {
    setRepRegPass(e.target.value);
  };

  const togglePass = (id) => {
    let element = document.getElementById(id);
    element.type = element.type === 'password' ? 'text' : 'password';
  }

  const showMessage = (id, buttonId) => {
    let messageBlock = document.getElementById(id);
    let button = document.getElementById(buttonId);
    button.disabled = true;
    messageBlock.style.opacity = 1;
    messageBlock.style.display = 'block';
    setTimeout(() => {
      messageBlock.classList.add('smoothOut');
      setTimeout(() => {
        messageBlock.style.display = 'none';
        messageBlock.classList.remove('smoothOut');
        button.disabled = false;
      }, 1000);
    }, 1500);
  }

  const handleRegSubmit = async (e) => {
    e.preventDefault();

    if (name.length === 0) {
      setMessage("Укажите имя");
      showMessage('messageReg', 'regButton');
    } else if (regLogin.length < 8) {
      setMessage("Логин слишком короткий");
      showMessage('messageReg', 'regButton');
    } else if (regPass.length < 8) {
      setMessage("Пароль слишком короткий");
      showMessage('messageReg', 'regButton');
    } else if (repRegPass.length < 8) {
      setMessage("Пароль слишком короткий");
      showMessage('messageReg', 'regButton');
    } else if (regPass !== repRegPass) {
      setMessage("Пароли не совпадают");
      showMessage('messageReg', 'regButton');
    } else {
      axios.post("http://localhost:8080/regUser", {
        name: name,
        login: regLogin,
        pass: regPass
      }).then(result => {
        if (result.data.state === "Error") {
          setMessage(result.data.message);
          showMessage('messageReg', 'regButton');
        } else {
          navigate("auth");
        }
      });
    }
  }

  return (
    <div className="registration">
      <img className="backLogo" src="login/back.PNG" alt="Back" 
        onClick={() => {
          navigate("auth");
        }}
      />
      <img className="registrationLogo" src="login/registration.PNG" alt="Registration"/>
      <div id="messageReg" style={{backgroundColor: message === "Логин свободен" ? "rgb(123, 225, 153)" : "rgb(252, 130, 130)"}}>
        <p>{message}</p>
      </div>

      <form onSubmit={handleRegSubmit}>
        <input className="loginInput registrationInput"
          placeholder="Введите имя"
          maxLength={30}
          value={name}
          onChange={handleName}
        />
        <input className="loginInput registrationInput"  
          placeholder="Введите логин"
          maxLength={16}
          value={regLogin}
          onChange={handleRegLogin}
        />
        <div className="password">
          <input id="#regPass" className="loginInput registrationInput" type="password"
            placeholder="Введите пароль"
            maxLength={16}
            value={regPass}
            onChange={handleRegPass}
          />
          <img className="passwordIcon" src="login/eye.jpg" alt="Eye"
            onClick={() => {
              togglePass('#regPass');
            }}
          />
        </div>
        <div className="password">
          <input id="#regPassRepeat" className="loginInput registrationInput" type="password"
            placeholder="Повторите пароль"
            maxLength={16}
            value={repRegPass}
            onChange={handleRepRegPass}
          />
          <img className="passwordIcon" src="login/eye.jpg" alt="Eye1"
            onClick={() => {
              togglePass('#regPassRepeat');
            }}
          />
        </div>
        <button id="regButton" className="buttonLogin" 
          type="submit"
        >
          Зарегистрироваться
        </button>
      </form>
    </div>
  );
}

export default Registration
