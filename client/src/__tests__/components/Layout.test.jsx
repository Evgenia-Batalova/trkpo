import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import axios from "axios";
import { MemoryRouter } from 'react-router';
import Layout from "../../components/Layout";
import * as user from "../../components/utilities/userContext";

function mockCallGet(data) {
  axios.get.mockResolvedValue({
    data: data
  });
}

const dataSuccess = {
  state: "Success",
  message: "",
  data: 2
}

describe('Layout tests', () => {
  const toggleNotifications = jest.fn();
  const signOut = jest.fn();

  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        id: 1,
        identificator: "user1",
        avatar: "image.jpg",
        toggleNotifications,
        signOut
      }
    });
  });

  afterEach(() => {
    axios.get.mockClear();
  });

  test('Layout Render', async () => {
    mockCallGet(dataSuccess);
    
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );
    
    await waitFor(() => {
      expect(screen.getByAltText("Exit")).toBeVisible();
    });
  });

  test('Layout signOut call', async () => {
    mockCallGet(dataSuccess);
    
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    await waitFor(() => {
      userEvent.click(screen.getByAltText("Exit"));
    });

    await waitFor(() => {
      expect(signOut).toHaveBeenCalledTimes(1);
    });
  });
});
