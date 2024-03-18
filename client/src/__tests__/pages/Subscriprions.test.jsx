import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import Subscriptions from "../../pages/Subscriptions";
import * as user from "../../components/utilities/userContext";

jest.mock('../../components/PostsList.jsx', () => ({ category }) => <p>{category}</p>);

describe('News tests', () => {
  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        id: 1
      }
    });
  });

  test('News Render', async () => {
    render(<Subscriptions />);

    await waitFor(() => {
      expect(screen.getByAltText("NarutoCat")).toBeVisible();
    });
  });

  test('Categories selection', async () => {
    render(<Subscriptions />);

    userEvent.click(screen.getByAltText("NarutoCat"));

    await waitFor(() => {
      expect(screen.getByText("1")).toBeVisible();
    });

    userEvent.click(screen.getByAltText("BleachCat"));

    await waitFor(() => {
      expect(screen.getByText("2")).toBeVisible();
    });

    userEvent.click(screen.getByAltText("PieceCat"));

    await waitFor(() => {
      expect(screen.getByText("3")).toBeVisible();
    });
  });
});