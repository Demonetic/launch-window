import { useQuery } from '@tanstack/react-query'
import { getBestViewingLaunches } from './launchApi'

export function useBestViewingLaunches() {
    return useQuery({
        queryKey: ['launches', 'best-viewing', 7, 3],
        queryFn: () => getBestViewingLaunches(7, 3),
    })
}