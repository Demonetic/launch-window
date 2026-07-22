import { useQuery } from '@tanstack/react-query'
import { getUpcomingLaunchCountries } from './launchApi'

export function useLaunchCountries() {
    return useQuery({
        queryKey: ['launches', 'countries'],
        queryFn: getUpcomingLaunchCountries,
        staleTime: 15 * 60 * 1000,
    })
}