import {
    useCallback,
    useMemo,
} from 'react'
import { useSearchParams } from 'react-router'
import {
    createLaunchFilterParams,
    parseLaunchFilterParams,
} from './launchFilterParams'
import type { LaunchFilters } from './types'

export function useLaunchFilterParams() {
    const [
        searchParameters,
        setSearchParameters,
    ] = useSearchParams()

    const filters = useMemo(
        () =>
            parseLaunchFilterParams(
                searchParameters,
            ),
        [searchParameters],
    )

    const setFilters = useCallback(
        (nextFilters: LaunchFilters) => {
            setSearchParameters(
                createLaunchFilterParams(
                    nextFilters,
                ),
                {
                    replace: true,
                },
            )
        },
        [setSearchParameters],
    )

    return {
        filters,
        setFilters,
    }
}