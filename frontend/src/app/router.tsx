import { createBrowserRouter } from 'react-router'
import App from '../App'
import { LoginPage } from '../features/auth/LoginPage'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'
import { RegisterPage } from '../features/auth/RegisterPage'
import { PlaceholderPage } from '../pages/PlaceholderPage'
import { UpcomingLaunchesPage } from '../features/launches/UpcomingLaunchesPage'
import { LaunchDetailPage } from '../features/launches/LaunchDetailPage'

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
                        element: <PlaceholderPage title="Calendar" />,
                    },
                    {
                        path: 'notes',
                        element: <PlaceholderPage title="Notes" />,
                    },
                    {
                        path: 'account',
                        element: <PlaceholderPage title="Account" />,
                    },
                ],
            },
            {
                path: '*',
                element: <PlaceholderPage title="Page not found" />,
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
])