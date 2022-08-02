import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { Outlet } from 'react-router-dom';
import { getAccessToken,
  getCurrentUser,
} from './features/Token';
import { fetchCurrentUser, checkLogin } from './features/user/UserSlice';
import NavBar from './components/common/NavBar';
import GlobalStyle from './styles/GlobalStyle';

function App() {
  const dispatch = useDispatch();
  useEffect(() => {
    const token = getAccessToken();
    if (token) {
      dispatch(fetchCurrentUser(getCurrentUser()));
      dispatch(checkLogin());
    }
  }, []);
  return (
    <div className="App">
      <GlobalStyle />
      <NavBar></NavBar>

      <Outlet></Outlet>
    </div>
  );
}

export default App;
