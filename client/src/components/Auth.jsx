import React, { useState } from "react";
import axios from 'axios';
import { useUser } from "../components/utilities/userContext";
import "../styles/login.css";

const Auth = ({ navigate, fromPage }) => {
  const user = useUser();
  const [message, setMessage] = useState("");
  const [authLogin, setAuthLogin] = useState("");
  const [authPass, setAuthPass] = useState("");

  const handleAuthLogin = (e) => {
    const value = e.target.value.replace(/[^a-z0-9]/gi, '');
    setAuthLogin(value);
  };

  const handleAuthPass = (e) => {
    setAuthPass(e.target.value);
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

  const handleLoginSubmit = async (e) => {
    e.preventDefault();

    await axios.get("http://localhost:8080/loginUser", {
      params: {
        login: authLogin,
        pass: authPass
      }
    }).then(result => {
      if (result.data.state === "Error") {
        setMessage(result.data.message);
        showMessage('messageLogin', 'loginButton');
      } else {
        user.signIn(result.data.data, () => navigate(fromPage), {replace: true});
      }
    });
  };

  return (
    <div className="authorization">
      <img className="authorizationLogo" src="login/authorization.PNG" alt="Authorization"/>
      <div id="messageLogin"><p>{message}</p></div>

      <form onSubmit={handleLoginSubmit}>
        <input className="loginInput"
          name="authLogin"
          placeholder="Введите логин" 
          maxLength={16}
          value={authLogin}
          onChange={handleAuthLogin}
        />
        <div className="password">
          <input id="#authPass" className="loginInput" type="password"
            name="authPass"
            placeholder="Введите пароль"
            maxLength={16}
            value={authPass}
            onChange={handleAuthPass}
          />
          <img className="passwordIcon" src="login/eye.jpg" alt="Eye"
            onClick={() => {
              togglePass('#authPass');
            }}
          />
        </div>
        <button id="loginButton" className="buttonLogin" 
          type="submit"
          disabled={((authLogin.length < 8) || (authPass.length < 8)) ? true : false}
          style={{marginTop: "1.5vw"}}
        >
          Войти
        </button>
      </form>
      <button id="loginButton" className="buttonLogin" style={{margin: "3vw auto 0"}}
        onClick={() => {
          navigate("register");
        }}
      >
        Регистрация
      </button>
    </div>
  );
}

export default Auth
