import {
    AtSign,
    LogOut,
    Mail,
    ShieldCheck,
    UserRound,
} from 'lucide-react'
import { AvatarPicker } from '../avatar/AvatarPicker'
import { UserAvatar } from '../avatar/UserAvatar'
import { useAuth } from '../auth/useAuth'
import { AccountDeletionPanel } from './AccountDeletionPanel'
import { useCurrentUser } from './useCurrentUser'
import './account.css'

export function AccountPage() {
    const { logout } = useAuth()

    const {
        data: user,
        error,
        isError,
        isPending,
    } = useCurrentUser()

    if (isPending) {
        return (
            <main className="account-page">
                <div className="account-state" role="status">
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
                <div>
                    <p className="page-eyebrow">Profile</p>
                    <h1>Your account</h1>
                    <p>
                        Your Launch Window identity and account
                        information.
                    </p>
                </div>
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
                            <small>Email</small>
                            <strong>{user.email}</strong>
                        </span>
                    </article>

                    <article>
                        <ShieldCheck aria-hidden="true" />
                        <span>
                            <small>Account role</small>
                            <strong>{user.role}</strong>
                        </span>
                    </article>

                    <article>
                        <AtSign aria-hidden="true" />
                        <span>
                            <small>Account ID</small>
                            <strong>#{user.id}</strong>
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