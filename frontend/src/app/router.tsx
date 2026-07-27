import { createBrowserRouter } from 'react-router'
import App from '../App'
import { AccountPage } from '../features/account/AccountPage'
import { ForgotPasswordPage } from '../features/auth/ForgotPasswordPage'
import { LoginPage } from '../features/auth/LoginPage'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'
import { RegisterPage } from '../features/auth/RegisterPage'
import { ResetPasswordPage } from '../features/auth/ResetPasswordPage'
import { CalendarPage } from '../features/calendar/CalendarPage'
import { FriendsPage } from '../features/friends/FriendsPage'
import { LaunchDetailPage } from '../features/launches/LaunchDetailPage'
import { UpcomingLaunchesPage } from '../features/launches/UpcomingLaunchesPage'
import { NotesPage } from '../features/notes/NotesPage'
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