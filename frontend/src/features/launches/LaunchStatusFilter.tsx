import type { LaunchStatus } from './types'

interface LaunchStatusFilterProps {
    statuses: LaunchStatus[]
    onToggle: (status: LaunchStatus) => void
}

interface StatusOption {
    value: LaunchStatus
    label: string
}

const STATUS_OPTIONS: StatusOption[] = [
    {
        value: 'GO',
        label: 'Go',
    },
    {
        value: 'TO_BE_CONFIRMED',
        label: 'To be confirmed',
    },
    {
        value: 'TO_BE_DETERMINED',
        label: 'To be determined',
    },
    {
        value: 'HOLD',
        label: 'Hold',
    },
]

export function LaunchStatusFilter({
                                       statuses,
                                       onToggle,
                                   }: LaunchStatusFilterProps) {
    return (
        <fieldset className="launch-status-filter">
            <legend>Launch status</legend>

            <div className="launch-status-options">
                {STATUS_OPTIONS.map((option) => {
                    const selected = statuses.includes(
                        option.value,
                    )

                    return (
                        <button
                            className={
                                selected
                                    ? 'launch-status-option selected'
                                    : 'launch-status-option'
                            }
                            type="button"
                            key={option.value}
                            aria-pressed={selected}
                            onClick={() =>
                                onToggle(option.value)
                            }
                        >
                            <span aria-hidden="true" />
                            {option.label}
                        </button>
                    )
                })}
            </div>
        </fieldset>
    )
}