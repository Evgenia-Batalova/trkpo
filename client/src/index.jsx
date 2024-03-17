import ReactDOM from 'react-dom/client';
import App from './components/App';
import { BrowserRouter } from 'react-router-dom';
import { UserProvider } from "./components/utilities/userContext";
import './styles/reset.css';

const root = ReactDOM.createRoot(
  document.getElementById('root')
);

const id = localStorage.getItem('userId');

root.render(
  <UserProvider ident={parseInt(id)}>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </UserProvider>
);
