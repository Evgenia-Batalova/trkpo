import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import axios from "axios";
import EditUserModal from "../../../components/modals/editUserModal";
import * as user from "../../../components/utilities/userContext";

const profileData = {
  identificator: "user1",
  imagePath: "Avatar.jpg",
  name: "naso337",
  info: "Some info"
}

function mockCallPut(data) {
  axios.put.mockResolvedValue({
    data: data
  });
}

describe('EditUserModal form tests', () => {
  const toggleNotifications = jest.fn();
  const setErrorMessage = jest.fn();
  const toggleError = jest.fn();
  const setRefreshedPost = jest.fn();
  const toggle = jest.fn();

  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        id: 1,
        notifOpen: true,
        toggleNotifications: toggleNotifications,
        setErrorMessage: setErrorMessage,
        toggleError: toggleError
      }
    });
  });

  afterEach(() => {
    axios.put.mockClear();
  });

  test('EditUserModal is not on the page', () => {
    render(<EditUserModal isOpen={false} data={profileData} />);
 
    expect(screen.queryByText("Some info")).toBeNull();
  });

  test('EditUserModal render', () => {
    render(<EditUserModal isOpen={true} data={profileData} />);
 
    expect(screen.queryByText("Some info")).toBeVisible();
  });

  test('EditUserModal inputs', async () => {
    render(<EditUserModal isOpen={true} data={profileData} />);
 
    await waitFor(() => {
      expect(screen.queryByText("Some info")).toBeVisible();
    });

    expect(screen.getByRole('button', { name: 'Сохранить' }).disabled).toBe(true);

    userEvent.type(screen.getByPlaceholderText("Введите имя"), "a1");
    userEvent.type(screen.getByPlaceholderText("Введите идентификатор"), "a1");
    userEvent.type(screen.getByPlaceholderText("Расскажите о себе"), "a1");

    expect(screen.getByRole('button', { name: 'Сохранить' }).disabled).toBe(false);
  });
});
