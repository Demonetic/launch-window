import { QueryClient } from '@tanstack/react-query'
import { ApiClientError } from '../lib/api'

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 60_000,
            refetchOnWindowFocus: false,
            retry: (failureCount, error) => {
                if (
                    error instanceof ApiClientError &&
                    error.status >= 400 &&
                    error.status < 500
                ) {
                    return false
                }

                return failureCount < 2
            },
        },
    },
})