import { createBrowserRouter } from 'react-router'
import App from '../App'
import { AccountPage } from '../features/account/AccountPage'
import { ForgotPasswordPage } from '../features/auth/pages/ForgotPasswordPage'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { ProtectedRoute } from '../features/auth/components/ProtectedRoute'
import { RegisterPage } from '../features/auth/pages/RegisterPage'
import { ResetPasswordPage } from '../features/auth/pages/ResetPasswordPage'
import { CalendarPage } from '../features/calendar/pages/CalendarPage'
import { FriendsPage } from '../features/friends/FriendsPage'
import { LaunchDetailPage } from '../features/launches/pages/LaunchDetailPage'
import { UpcomingLaunchesPage } from '../features/launches/pages/UpcomingLaunchesPage'
import { NotesPage } from '../features/notes/pages/NotesPage'
import { MessagesPage } from '../features/notifications/MessagesPage'
import { PlaceholderPage } from '../pages/PlaceholderPage'

export const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                index: true,
                element: <UpcomingLaunchesPage />,
            },
            {
                path: 'launches/:launchId',
                element: <LaunchDetailPage />,
            },
            {
                element: <ProtectedRoute />,
                children: [
                    {
                        path: 'calendar',
                        element: <CalendarPage />,
                    },
                    {
                        path: 'notes',
                        element: <NotesPage />,
                    },
                    {
                        path: 'messages',
                        element: <MessagesPage />,
                    },
                    {
                        path: 'friends',
                        element: <FriendsPage />,
                    },
                    {
                        path: 'account',
                        element: <AccountPage />,
                    },
                ],
            },
            {
                path: '*',
                element: (
                    <PlaceholderPage title="Page not found" />
                ),
            },
        ],
    },
    {
        path: 'login',
        element: <LoginPage />,
    },
    {
        path: 'register',
        element: <RegisterPage />,
    },
    {
        path: 'forgot-password',
        element: <ForgotPasswordPage />,
    },
    {
        path: 'reset-password',
        element: <ResetPasswordPage />,
    },
])