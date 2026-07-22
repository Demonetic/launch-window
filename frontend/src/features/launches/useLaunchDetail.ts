import { useQuery } from '@tanstack/react-query'
import {
    getLaunch,
    getLaunchWeather,
} from './launchApi'

export function useLaunchDetail(launchId: number | null) {
    const isValidLaunchId =
        launchId !== null &&
        Number.isSafeInteger(launchId) &&
        launchId > 0

    const launchQuery = useQuery({
        queryKey: ['launches', 'detail', launchId],
        queryFn: () => getLaunch(launchId!),
        enabled: isValidLaunchId,
    })

    const weatherQuery = useQuery({
        queryKey: ['launches', 'weather', launchId],
        queryFn: () => getLaunchWeather(launchId!),
        enabled: isValidLaunchId,
    })

    return {
        isValidLaunchId,
        launchQuery,
        weatherQuery,
    }
}