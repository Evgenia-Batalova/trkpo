import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import ErrorModal from "../../../components/modals/errorModal";
import * as user from "../../../components/utilities/userContext";

describe('ErrorModal tests', () => {
  beforeEach(() => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        errorOpen: true,
        toggleError: (() => errorOpen = !errorOpen),
        errorMessage: "Не удалось обработать запрос"
      }
    });
  });

  test('ErrorModal render', () => {
    render(
      <ErrorModal />
    );

    expect(screen.getByText("Не удалось обработать запрос")).toBeVisible();
  });

  test('ErrorModal not rendering', () => {
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        errorOpen: false,
        toggleError: (() => errorOpen = !errorOpen),
        errorMessage: "Не удалось обработать запрос"
      }
    });

    render(
      <ErrorModal />
    );

    expect(screen.queryByText("Не удалось обработать запрос")).toBeNull();
  });

  test('ErrorModal don\'t close on click', () => {
    const toggleError = jest.fn();
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        errorOpen: true,
        toggleError,
        errorMessage: "Не удалось обработать запрос"
      }
    });

    render(
      <ErrorModal />
    );

    userEvent.click(document.getElementsByClassName('modalContainer')[0]);
    expect(toggleError).toHaveBeenCalledTimes(0);
  });

  test('ErrorModal close', () => {
    const toggleError = jest.fn();
    jest.spyOn(user, 'useUser').mockImplementation(() => {
      return {
        errorOpen: true,
        toggleError,
        errorMessage: "Не удалось обработать запрос"
      }
    });

    render(
      <ErrorModal />
    );

    userEvent.click(document.getElementsByClassName('overlay')[0]);
    expect(toggleError).toHaveBeenCalledTimes(1);
  });

});
