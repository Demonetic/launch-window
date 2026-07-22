import {
    CloudSun,
    Sparkles,
} from 'lucide-react'
import { BestViewingCard } from './BestViewingCard'
import { useBestViewingLaunches } from './useBestViewingLaunches'
import './bestViewing.css'

export function BestViewingSection() {
    const {
        data: launches,
        isError,
        isPending,
    } = useBestViewingLaunches()

    return (
        <section
            className="best-viewing-section"
            aria-labelledby="best-viewing-heading"
        >
            <header className="best-viewing-header">
                <div className="best-viewing-title">
                    <span>
                        <Sparkles
                            aria-hidden="true"
                            size={20}
                        />
                    </span>

                    <div>
                        <p className="page-eyebrow">
                            Next seven days
                        </p>
                        <h2 id="best-viewing-heading">
                            Best viewing conditions
                        </h2>
                    </div>
                </div>

                <p>
                    Upcoming launches ranked by their
                    forecast viewing score.
                </p>
            </header>

            {isPending && (
                <div
                    className="best-viewing-state"
                    role="status"
                >
                    <span className="launch-loader" />
                    <span>
                        Finding the clearest launch windows...
                    </span>
                </div>
            )}

            {isError && (
                <div
                    className="best-viewing-state best-viewing-error"
                    role="alert"
                >
                    Viewing recommendations are temporarily
                    unavailable.
                </div>
            )}

            {!isPending &&
                !isError &&
                launches?.length === 0 && (
                    <div className="best-viewing-state">
                        <CloudSun
                            aria-hidden="true"
                            size={28}
                        />
                        <span>
                            No launch forecasts are available
                            for the next seven days.
                        </span>
                    </div>
                )}

            {launches && launches.length > 0 && (
                <div className="best-viewing-grid">
                    {launches.map((launch) => (
                        <BestViewingCard
                            key={launch.id}
                            launch={launch}
                        />
                    ))}
                </div>
            )}
        </section>
    )
}