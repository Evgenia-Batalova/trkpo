import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import axios from "axios";
import Auth from "../../components/Auth";
import * as user from "../../components/utilities/userContext";

const inputString = "naso@()*&137nasonasoaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

function mockCall(data) {
  axios.get.mockResolvedValue({
    data: data
  });
}

describe('Authorization form tests', () => {
  afterEach(() => {
    axios.get.mockClear();
  });

  test('Check render Sign-in button', () => {
    render(<Auth />);
    expect(screen.getByText("Войти")).toBeInTheDocument();
  })

  test('Check render Sign-up button', () => {
    render(<Auth />);
    expect(screen.getByText("Регистрация")).toBeInTheDocument();
  })

  test('Check correct input for login', () => {
    render(<Auth />);

    const input = screen.getByPlaceholderText('Введите логин');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(16);
    expect(input.value).toBe("naso137nasonasoa");
  });

  test('Check correct input for password', () => {
    render(<Auth />);

    const input = screen.getByPlaceholderText('Введите пароль');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(16);
  });

  test('Check password hiding', () => {
    render(<Auth />);

    const input = screen.getByPlaceholderText('Введите пароль');
    userEvent.type(input, inputString);
    
    expect(screen.queryByText("naso@()*&137naso")).toBeNull();
  });

  test('Check \'Show Password\' button', () => {
    render(<Auth />);

    const input = screen.getByPlaceholderText('Введите пароль');
    userEvent.type(input, inputString);

    userEvent.click(screen.getByAltText("Eye"));

    expect(input.type).toBe('text');
  });

  test('Check button on disability', () => {
    render(<Auth />);
    const button = screen.getByRole('button', { name: 'Войти'});
    expect(button.disabled).toBe(true);
  });

  test('Check button on availability', () => {
    render(<Auth />);
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const button = screen.getByRole('button', { name: 'Войти'});
    userEvent.type(inputLogin, inputString);
    userEvent.type(inputPassword, inputString);
    
    expect(button.disabled).toBe(false);
  });

  test('Check login button work', async () => {
    const data = {
      state: "Error",
      message: "Не удалось войти",
      data: -1
    }
    mockCall(data);

    render(<Auth />);
    
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const loginButton = screen.getByRole('button', { name: 'Войти'});
    userEvent.type(inputLogin, inputString);
    userEvent.type(inputPassword, inputString);

    await waitFor(() => {
      userEvent.click(loginButton);
    });

    setTimeout(() => {
      expect(screen.getByText('Не удалось войти')).toBeInTheDocument();
    }, 500);
  });

  test('Check user signing in', async () => {
    const data = {
      state: "Success",
      message: "Успешный вход",
      data: 1
    }
    
    const signIn = jest.fn();

    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {signIn: signIn}
    });

    mockCall(data);

    render(<Auth />);
    
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const loginButton = screen.getByRole('button', { name: 'Войти'});
    userEvent.type(inputLogin, inputString);
    userEvent.type(inputPassword, inputString);

    await waitFor(() => userEvent.click(loginButton));

    expect(signIn).toHaveBeenCalledTimes(1);
  });

  test('Navigation to registration', () => {
    const navigate = jest.fn();

    render(<Auth navigate={navigate} />);
    const button = screen.getByRole('button', { name: 'Регистрация'});

    userEvent.click(button);
    
    expect(navigate).toHaveBeenCalledTimes(1);
  });

});

describe("Auth login input tests", () => {
  let loginInput;
  let loginBtn;

  beforeEach(() => {
    render(<Auth />);
    loginInput = screen.getByPlaceholderText('Введите логин');
    loginBtn = screen.getByRole('button', { name: 'Войти' });

    userEvent.type(screen.getByPlaceholderText('Введите пароль'), "nasonaso");
  });

  test('Login input with 7 chars', () => {
    userEvent.type(loginInput, "nasonas");

    expect(loginInput.value.length).toBe(7);
    expect(loginBtn.disabled).toBe(true);
  });

  test('Login input with 8 chars', () => {
    userEvent.type(loginInput, "nasonaso");

    expect(loginInput.value.length).toBe(8);
    expect(loginBtn.disabled).toBe(false);
  });

  test('Login input with 16 chars', () => {
    userEvent.type(loginInput, "nasonasonasonaso");

    expect(loginInput.value.length).toBe(16);
    expect(loginBtn.disabled).toBe(false);
  });

  test('Login input with 17 chars', () => {
    userEvent.type(loginInput, "nasonasonasonason");

    expect(loginInput.value.length).toBe(16);
    expect(loginBtn.disabled).toBe(false);
  });

  test('Login input with 10 chars', () => {
    userEvent.type(loginInput, "nasonasona");

    expect(loginInput.value.length).toBe(10);
    expect(loginBtn.disabled).toBe(false);
  });
});
