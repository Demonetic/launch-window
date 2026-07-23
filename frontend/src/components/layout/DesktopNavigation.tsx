import {
    LogIn,
    LogOut,
    Orbit,
} from 'lucide-react'
import {
    Link,
    NavLink,
} from 'react-router'
import {useAuth} from '../../features/auth/useAuth'
import {navigationItems} from './navigationItems'
import { UserAvatar } from '../../features/avatar/UserAvatar'

export function DesktopNavigation() {
    const {
        user,
        isAuthenticated,
        logout,
    } = useAuth()

    return (
        <aside className="desktop-navigation">
            <Link className="app-brand" to="/">
        <span className="app-brand-icon">
          <Orbit aria-hidden="true" size={22}/>
        </span>

                <span>
          <strong>Launch Window</strong>
          <small>Mission control</small>
        </span>
            </Link>

            <nav
                className="desktop-navigation-links"
                aria-label="Primary navigation"
            >
                {navigationItems.map((item) => {
                    const Icon = item.icon

                    return (
                        <NavLink
                            className={({isActive}) =>
                                `navigation-link${isActive ? ' active' : ''}`
                            }
                            end={item.end}
                            key={item.to}
                            to={item.to}
                        >
                            <Icon aria-hidden="true" size={20}/>
                            <span>{item.label}</span>
                        </NavLink>
                    )
                })}
            </nav>

            <div className="desktop-account">
                {isAuthenticated && user ? (
                    <>
                        <Link
                            className="account-summary"
                            to="/account"
                        >
                            <UserAvatar
                                avatarKey={user.avatarKey}
                                avatarColor={user.avatarColor}
                                size="small"
                            />

                            <span>
    <strong>{user.username}</strong>
    <small>{user.email}</small>
  </span>
                        </Link>

                        <button
                            className="account-action"
                            onClick={logout}
                            type="button"
                        >
                            <LogOut aria-hidden="true" size={18}/>
                            Log out
                        </button>
                    </>
                ) : (
                    <Link className="account-action" to="/login">
                        <LogIn aria-hidden="true" size={18}/>
                        Log in
                    </Link>
                )}
            </div>
        </aside>
    )
}