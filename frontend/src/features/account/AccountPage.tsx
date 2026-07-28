import {
    CalendarDays,
    LogOut,
    Mail,
    NotebookPen,
    UserRound,
    UsersRound,
} from 'lucide-react'
import { AvatarPicker } from '../avatar/AvatarPicker'
import { UserAvatar } from '../avatar/UserAvatar'
import { useAuth } from '../auth/session/useAuth'
import { AccountDeletionPanel } from './AccountDeletionPanel'
import { useCurrentUser } from './useCurrentUser'
import { useUserStatistics } from './useUserStatistics'
import './account.css'

function formatStatistic(value: number | undefined) {
    if (value === undefined) {
        return '—'
    }

    return new Intl.NumberFormat('en').format(value)
}

export function AccountPage() {
    const { logout } = useAuth()

    const {
        data: user,
        error,
        isError,
        isPending,
    } = useCurrentUser()

    const {
        data: statistics,
        isError: isStatisticsError,
        isPending: isStatisticsPending,
    } = useUserStatistics()

    if (isPending) {
        return (
            <main className="account-page">
                <div
                    className="account-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <p>Loading your account...</p>
                </div>
            </main>
        )
    }

    if (isError) {
        return (
            <main className="account-page">
                <div
                    className="account-state account-error"
                    role="alert"
                >
                    <h1>Account unavailable</h1>

                    <p>
                        {error instanceof Error
                            ? error.message
                            : 'Your account could not be loaded.'}
                    </p>
                </div>
            </main>
        )
    }

    return (
        <main className="account-page">
            <header className="account-header">
                <p className="page-eyebrow">
                    Profile
                </p>

                <h1>Your account</h1>

                <p>
                    Your Launch Window identity,
                    activity and profile settings.
                </p>
            </header>

            <section className="account-profile-card">
                <div className="account-profile-heading">
                    <UserAvatar
                        avatarKey={user.avatarKey}
                        avatarColor={user.avatarColor}
                        size="medium"
                    />

                    <div>
                        <p>Launch Window member</p>
                        <h2>{user.username}</h2>
                    </div>
                </div>

                <div
                    className="account-statistics"
                    aria-label="Account activity"
                >
                    <article>
                        <CalendarDays
                            aria-hidden="true"
                            size={21}
                        />

                        <span>
                            <strong>
                                {formatStatistic(
                                    statistics?.savedLaunches,
                                )}
                            </strong>
                            <small>Saved launches</small>
                        </span>
                    </article>

                    <article>
                        <NotebookPen
                            aria-hidden="true"
                            size={21}
                        />

                        <span>
                            <strong>
                                {formatStatistic(
                                    statistics?.notesWritten,
                                )}
                            </strong>
                            <small>Notes written</small>
                        </span>
                    </article>

                    <article>
                        <UsersRound
                            aria-hidden="true"
                            size={21}
                        />

                        <span>
                            <strong>
                                {formatStatistic(
                                    statistics?.friends,
                                )}
                            </strong>
                            <small>Friends</small>
                        </span>
                    </article>
                </div>

                {isStatisticsPending && (
                    <p
                        className="account-statistics-message"
                        role="status"
                    >
                        Loading account activity...
                    </p>
                )}

                {isStatisticsError && (
                    <p
                        className="account-statistics-message account-statistics-error"
                        role="alert"
                    >
                        Account activity could not be
                        loaded.
                    </p>
                )}

                <AvatarPicker
                    currentAvatarKey={user.avatarKey}
                    currentAvatarColor={user.avatarColor}
                />

                <div className="account-information">
                    <article>
                        <UserRound aria-hidden="true" />

                        <span>
                            <small>Username</small>
                            <strong>{user.username}</strong>
                        </span>
                    </article>

                    <article>
                        <Mail aria-hidden="true" />

                        <span>
                            <small>Email address</small>
                            <strong>{user.email}</strong>
                        </span>
                    </article>
                </div>

                <div className="account-profile-footer">
                    <div>
                        <h3>End this session</h3>

                        <p>
                            You will be signed out on this
                            device. Your saved calendar and
                            notes will not be deleted.
                        </p>
                    </div>

                    <button
                        className="account-logout-button"
                        type="button"
                        onClick={logout}
                    >
                        <LogOut
                            aria-hidden="true"
                            size={18}
                        />
                        Log out
                    </button>
                </div>
            </section>

            <AccountDeletionPanel />
        </main>
    )
}