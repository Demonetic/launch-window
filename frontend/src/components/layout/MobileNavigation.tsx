import {
    LogIn,
    UserRound,
} from 'lucide-react'
import { NavLink } from 'react-router'
import { useAuth } from '../../features/auth/useAuth'
import { navigationItems } from './navigationItems'

export function MobileNavigation() {
    const { isAuthenticated } = useAuth()

    return (
        <nav
            className="mobile-navigation"
            aria-label="Mobile navigation"
        >
            {navigationItems.map((item) => {
                const Icon = item.icon

                return (
                    <NavLink
                        className={({ isActive }) =>
                            `mobile-navigation-link${isActive ? ' active' : ''}`
                        }
                        end={item.end}
                        key={item.to}
                        to={item.to}
                    >
                        <Icon aria-hidden="true" size={21} />
                        <span>{item.label}</span>
                    </NavLink>
                )
            })}

            <NavLink
                className={({ isActive }) =>
                    `mobile-navigation-link${isActive ? ' active' : ''}`
                }
                to={isAuthenticated ? '/account' : '/login'}
            >
                {isAuthenticated ? (
                    <UserRound aria-hidden="true" size={21} />
                ) : (
                    <LogIn aria-hidden="true" size={21} />
                )}

                <span>
          {isAuthenticated ? 'Account' : 'Log in'}
        </span>
            </NavLink>
        </nav>
    )
}