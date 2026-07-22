import {
    CalendarCheck,
    CalendarPlus,
    LoaderCircle,
} from 'lucide-react'
import {
    useLocation,
    useNavigate,
} from 'react-router'
import { useSavedLaunch } from './useSavedLaunch'

interface CalendarToggleButtonProps {
    launchId: number
}

export function CalendarToggleButton({
                                         launchId,
                                     }: CalendarToggleButtonProps) {
    const location = useLocation()
    const navigate = useNavigate()

    const {
        isAuthenticated,
        isError,
        isLoading,
        isPending,
        isSaved,
        toggle,
    } = useSavedLaunch(launchId)

    function handleClick() {
        if (!isAuthenticated) {
            void navigate('/login', {
                state: {
                    from: location.pathname,
                },
            })
            return
        }

        toggle()
    }

    const isBusy = isLoading || isPending

    return (
        <div className="calendar-toggle">
            <button
                className={`calendar-toggle-button${
                    isSaved ? ' calendar-toggle-button-saved' : ''
                }`}
                type="button"
                disabled={isBusy}
                onClick={handleClick}
            >
                {isBusy ? (
                    <LoaderCircle
                        className="calendar-toggle-spinner"
                        aria-hidden="true"
                        size={18}
                    />
                ) : isSaved ? (
                    <CalendarCheck
                        aria-hidden="true"
                        size={18}
                    />
                ) : (
                    <CalendarPlus
                        aria-hidden="true"
                        size={18}
                    />
                )}

                {!isAuthenticated
                    ? 'Sign in to save'
                    : isSaved
                        ? 'Remove from calendar'
                        : 'Save to calendar'}
            </button>

            {isError && (
                <p role="alert">
                    Calendar could not be updated.
                </p>
            )}
        </div>
    )
}