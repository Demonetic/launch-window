import {
    LogIn,
    UserRound,
} from 'lucide-react'
import { NavLink } from 'react-router'
import { useAuth } from '../../features/auth/useAuth'
import { usePendingCalendarInvitations } from '../../features/calendar/usePendingCalendarInvitations'
import { navigationItems } from './navigationItems'

function formatNotificationCount(count: number) {
    return count > 99 ? '99+' : String(count)
}

export function MobileNavigation() {
    const { isAuthenticated } = useAuth()

    const { count: invitationCount } =
        usePendingCalendarInvitations()

    return (
        <nav
            className="mobile-navigation"
            aria-label="Mobile navigation"
        >
            {navigationItems.map((item) => {
                const Icon = item.icon

                const showsInvitations =
                    item.to === '/calendar' &&
                    invitationCount > 0

                const notificationLabel =
                    showsInvitations
                        ? `, ${invitationCount} pending ${
                            invitationCount === 1
                                ? 'invitation'
                                : 'invitations'
                        }`
                        : ''

                return (
                    <NavLink
                        aria-label={`${item.label}${notificationLabel}`}
                        className={({ isActive }) =>
                            `mobile-navigation-link${
                                isActive
                                    ? ' active'
                                    : ''
                            }`
                        }
                        end={item.end}
                        key={item.to}
                        to={item.to}
                    >
                        <span className="navigation-icon-container">
                            <Icon
                                aria-hidden="true"
                                size={21}
                            />

                            {showsInvitations && (
                                <span
                                    className="navigation-notification-badge"
                                    aria-hidden="true"
                                >
                                    {formatNotificationCount(
                                        invitationCount,
                                    )}
                                </span>
                            )}
                        </span>

                        <span>{item.label}</span>
                    </NavLink>
                )
            })}

            <NavLink
                className={({ isActive }) =>
                    `mobile-navigation-link${
                        isActive ? ' active' : ''
                    }`
                }
                to={
                    isAuthenticated
                        ? '/account'
                        : '/login'
                }
            >
                {isAuthenticated ? (
                    <UserRound
                        aria-hidden="true"
                        size={21}
                    />
                ) : (
                    <LogIn
                        aria-hidden="true"
                        size={21}
                    />
                )}

                <span>
                    {isAuthenticated
                        ? 'Account'
                        : 'Log in'}
                </span>
            </NavLink>
        </nav>
    )
}