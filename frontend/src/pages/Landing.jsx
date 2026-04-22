import { Link } from 'react-router-dom';
import './Landing.css';

export default function Landing() {
  return (
    <div className="landing">
      {/* Floating gradient orbs */}
      <div className="orb orb-1" />
      <div className="orb orb-2" />
      <div className="orb orb-3" />

      {/* Navigation */}
      <nav className="landing-nav">
        <div className="landing-brand">
          <div className="brand-icon">💬</div>
          <span className="brand-name">Secure<span>Chat</span></span>
        </div>
        <div className="nav-actions">
          <Link to="/login" className="nav-btn nav-btn-ghost">Sign In</Link>
          <Link to="/register" className="nav-btn nav-btn-primary">Get Started</Link>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero">
        <div className="hero-badge">🔒 End-to-End Encrypted</div>
        <h1>
          Private conversations,<br />
          <span className="gradient-text">truly private.</span>
        </h1>
        <p className="hero-sub">
          SecureChat uses RSA + AES-256 encryption so your messages can only be
          read by you and the person you're talking to. Not even we can read them.
        </p>
        <div className="hero-cta">
          <Link to="/register" className="cta-primary">
            Start Chatting Free
            <span className="cta-arrow">→</span>
          </Link>
          <Link to="/login" className="cta-secondary">
            I have an account
          </Link>
        </div>
      </section>

      {/* Features */}
      <section className="features">
        <div className="feature-card">
          <div className="feature-icon">🔐</div>
          <h3>E2E Encrypted</h3>
          <p>RSA-2048 key exchange with AES-256-GCM message encryption. Zero-knowledge architecture.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">⚡</div>
          <h3>Real-time</h3>
          <p>WebSocket-powered instant messaging. See messages the moment they're sent.</p>
        </div>
        <div className="feature-card">
          <div className="feature-icon">👥</div>
          <h3>Group Chats</h3>
          <p>Create groups, add members, and have secure conversations with your team.</p>
        </div>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <p>Built with React, Spring Boot & Supabase</p>
      </footer>
    </div>
  );
}
