import { createBrowserRouter } from 'react-router'
import App from '../App'
import { PlaceholderPage } from '../pages/PlaceholderPage'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'

export const router = createBrowserRouter([
    {
        element: <App />,
        children: [
            {
                index: true,
                element: <PlaceholderPage title="Launch Window" />,
            },
            {
                path: 'launches/:launchId',
                element: <PlaceholderPage title="Launch details" />,
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
                ],
            },
            {
                path: 'login',
                element: <PlaceholderPage title="Log in" />,
            },
            {
                path: 'register',
                element: <PlaceholderPage title="Create account" />,
            },
            {
                path: '*',
                element: <PlaceholderPage title="Page not found" />,
            },
        ],
    },
])