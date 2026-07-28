export type LaunchStatus =
    | 'GO'
    | 'TO_BE_DETERMINED'
    | 'TO_BE_CONFIRMED'
    | 'HOLD'
    | 'IN_FLIGHT'
    | 'SUCCESS'
    | 'PARTIAL_FAILURE'
    | 'FAILURE'
    | 'UNKNOWN'

export type LaunchSort = 'SOONEST' | 'BEST_VIEWING'

export interface LaunchFilters {
    sort: LaunchSort
    days: number | null
    statuses: LaunchStatus[]
    countryCodes: string[]
    query: string
    forecastAvailable: boolean | null
    minimumViewingScore: number | null
}

export type ViewingCondition =
    | 'EXCELLENT'
    | 'GOOD'
    | 'FAIR'
    | 'POOR'
    | 'VERY_POOR'

export interface WeatherSummary {
    viewingScore: number
    viewingCondition: ViewingCondition
    forecastTime: string
}

export interface LaunchCountry {
    code: string
    name: string
}

export interface LaunchSummary {
    id: number
    name: string
    status: LaunchStatus
    launchTime: string
    imageUrl: string | null
    rocketName: string | null
    organizationName: string | null
    padName: string | null
    locationName: string | null
    countryCode: string | null
    countryName: string | null
    weather: WeatherSummary | null
}

export interface LaunchCursor {
    afterTime: string
    afterId: number
    afterViewingScore: number | null
}

export interface LaunchPage {
    items: LaunchSummary[]
    nextCursor: LaunchCursor | null
    hasNext: boolean
}

export interface LaunchDetail {
    id: number
    name: string
    description: string | null
    status: LaunchStatus
    launchTime: string
    imageUrl: string | null
    webcastUrl: string | null
    rocketName: string | null
    missionType: string | null
    organizationName: string | null
    padName: string | null
    locationName: string | null
    countryCode: string | null
    countryName: string | null
    latitude: number | null
    longitude: number | null
    lastSyncedAt: string
}

export interface LaunchWeather {
    launchId: number
    forecastTime: string
    temperatureC: number
    cloudCoverPercent: number
    precipitationProbabilityPercent: number
    windSpeedKmh: number
    visibilityMeters: number | null
    viewingScore: number
    viewingCondition: ViewingCondition
    fetchedAt: string
}