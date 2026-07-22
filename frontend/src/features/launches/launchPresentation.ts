import type {
    LaunchStatus,
    ViewingCondition,
} from './types'

export const statusLabels: Record<LaunchStatus, string> = {
    GO: 'Go',
    TO_BE_DETERMINED: 'TBD',
    TO_BE_CONFIRMED: 'TBC',
    HOLD: 'On hold',
    IN_FLIGHT: 'In flight',
    SUCCESS: 'Success',
    PARTIAL_FAILURE: 'Partial failure',
    FAILURE: 'Failure',
    UNKNOWN: 'Unknown',
}

export const viewingLabels: Record<ViewingCondition, string> = {
    EXCELLENT: 'Excellent viewing',
    GOOD: 'Good viewing',
    FAIR: 'Fair viewing',
    POOR: 'Poor viewing',
    VERY_POOR: 'Very poor viewing',
}

export function formatDateTime(value: string) {
    return new Intl.DateTimeFormat('en', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(new Date(value))
}

export function toClassName(value: string) {
    return value.toLowerCase().replaceAll('_', '-')
}