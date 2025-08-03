import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Header from './components/Header';
import Home from './pages/Home';
import Dashboard from './pages/Dashboard';
import './styles/App.css';
import Auth from './pages/Auth';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app app-bg">
          <Header />
          <main>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/dashboard/*" element={<Dashboard />} />
              <Route path="/auth" element={<Auth />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
