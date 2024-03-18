import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import axios from "axios";
import Registration from "../../components/Registration";

const inputString = "naso@()*&137nasonasoaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

function mockCall(data) {
  axios.post.mockResolvedValue({
    data: data
  });
}

function mockCallGet(data) {
  axios.get.mockResolvedValue({
    data
  });
}

describe('Registration form tests', () => {
  afterEach(() => {
    axios.get.mockClear();
    axios.post.mockClear();
  });

  test('Check render Back button', () => {
    render(<Registration />);
    expect(screen.getByAltText("Back")).toBeInTheDocument();
  });

  test('Check correct input for name', () => {
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите имя');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(30);
    expect(input.value).toBe("naso137nasonasoaaaaaaaaaaaaaaa");
  });


  test('Check correct input for login', () => {
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите логин');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(16);
    expect(input.value).toBe("naso137nasonasoa");
  });

  test('Check correct input for password', () => {
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите пароль');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(16);
  });

  test('Check correct input for repeated password', () => {
    render(<Registration />);

    const input = screen.getByPlaceholderText('Повторите пароль');
    userEvent.type(input, inputString);

    expect(input.value.length).toBe(16);
  });

  test('Check message showing when inputs is empty', () => {
    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});

    userEvent.click(button);
    expect(screen.getByText("Укажите имя")).toBeInTheDocument();
  });

  test('Check message showing when login is empty', () => {
    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');

    userEvent.type(inputName, inputString);
    userEvent.click(button);

    expect(screen.getByText("Логин слишком короткий")).toBeInTheDocument();
  });

  test('Check message showing when password is empty', () => {
    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');
    const inputLogin = screen.getByPlaceholderText('Введите логин');

    userEvent.type(inputName, inputString);
    userEvent.type(inputLogin, inputString);
    userEvent.click(button);
    expect(screen.getByText("Пароль слишком короткий")).toBeInTheDocument();
  });

  test('Check message showing when second password is empty', () => {
    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');

    userEvent.type(inputName, inputString);
    userEvent.type(inputLogin, inputString);
    userEvent.type(inputPassword, inputString);
    userEvent.click(button);

    expect(screen.getByText("Пароль слишком короткий")).toBeInTheDocument();
  });

  test('Check message showing when passwords don\'t match', () => {
    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const inputRepeatedPassword = screen.getByPlaceholderText('Повторите пароль');

    userEvent.type(inputName, inputString);
    userEvent.type(inputLogin, inputString);   
    userEvent.type(inputPassword, inputString);    
    userEvent.type(inputRepeatedPassword, 'abrakadabra');
    userEvent.click(button);

    expect(screen.getByText("Пароли не совпадают")).toBeInTheDocument();
  });

  test('Check message showing when unable to register', async () => {
    const data = {
      state: "Error",
      message: "Не удалось зарегистрироваться",
      data: -1
    }

    render(<Registration />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const inputRepeatedPassword = screen.getByPlaceholderText('Повторите пароль');

    userEvent.type(inputName, inputString);
    userEvent.type(inputLogin, inputString);   
    userEvent.type(inputPassword, inputString);    
    userEvent.type(inputRepeatedPassword, inputString);

    mockCall(data);

    await waitFor(() => {
      userEvent.click(button);
      expect(screen.getByText("Не удалось зарегистрироваться")).toBeInTheDocument();
    });
  });

  test('Navigation to login after successful reg', async () => {
    const data = {
      state: "Success",
      message: "Регистрация прошла успешно",
      data: 1
    }

    const navigate = jest.fn();

    render(<Registration navigate={navigate} />);
    const button = screen.getByRole('button', { name: 'Зарегистрироваться'});
    const inputName = screen.getByPlaceholderText('Введите имя');
    const inputLogin = screen.getByPlaceholderText('Введите логин');
    const inputPassword = screen.getByPlaceholderText('Введите пароль');
    const inputRepeatedPassword = screen.getByPlaceholderText('Повторите пароль');

    userEvent.type(inputName, inputString);
    userEvent.type(inputLogin, inputString);   
    userEvent.type(inputPassword, inputString);    
    userEvent.type(inputRepeatedPassword, inputString);

    mockCall(data);

    await waitFor(() => userEvent.click(button));
    
    expect(navigate).toHaveBeenCalledTimes(1);
  });

  test('Check \'Show Password\' button', () => {
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите пароль');
    const secondInput = screen.getByPlaceholderText('Повторите пароль');
    userEvent.type(input, inputString);
    userEvent.type(secondInput, inputString);

    userEvent.click(screen.getByAltText("Eye"));
    userEvent.click(screen.getByAltText("Eye1"));

    expect(input.type).toBe('text');
    expect(secondInput.type).toBe('text');
  });

  test('Check \'Verify login\' button when login is free', async () => {
    const data = {
      state: "Success",
      message: "Логин свободен",
      data: true
    }

    mockCallGet(data);
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите логин');
    userEvent.type(input, inputString);

    setTimeout(() => {
      expect(screen.getByText("Логин свободен")).toBeInTheDocument();
    }, 500);
  });

  test('Check \'Verify login\' button when login is not free', async () => {
    const data = {
      state: "Success",
      message: "Логин занят",
      data: false
    }

    mockCallGet(data);
    render(<Registration />);

    const input = screen.getByPlaceholderText('Введите логин');
    userEvent.type(input, inputString);

    setTimeout(() => {
      expect(screen.getByText("Логин занят")).toBeInTheDocument();
    }, 500);
  });

  test('Navigation to login by click on back button', () => {
    const navigate = jest.fn();

    render(<Registration navigate={navigate} />);
    const button = screen.getByAltText('Back');

    userEvent.click(button);
    expect(navigate).toHaveBeenCalledTimes(1);
  });
  
});
