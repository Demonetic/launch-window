import {
    MapPin,
    X,
} from 'lucide-react'
import { useLaunchCountries } from './useLaunchCountries'
import './launchCountryFilter.css'

interface LaunchCountryFilterProps {
    selectedCodes: string[]
    onChange: (countryCodes: string[]) => void
}

export function LaunchCountryFilter({
                                        selectedCodes,
                                        onChange,
                                    }: LaunchCountryFilterProps) {
    const {
        data: countries = [],
        isError,
        isPending,
    } = useLaunchCountries()

    const selectedCountries = selectedCodes.map(
        (code) =>
            countries.find(
                (country) => country.code === code,
            ) ?? {
                code,
                name: code,
            },
    )

    const availableCountries = countries.filter(
        (country) =>
            !selectedCodes.includes(country.code),
    )

    function addCountry(countryCode: string) {
        if (
            !countryCode ||
            selectedCodes.includes(countryCode)
        ) {
            return
        }

        onChange([
            ...selectedCodes,
            countryCode,
        ])
    }

    function removeCountry(countryCode: string) {
        onChange(
            selectedCodes.filter(
                (code) => code !== countryCode,
            ),
        )
    }

    function placeholder() {
        if (isPending) {
            return 'Loading countries...'
        }

        if (isError) {
            return 'Countries unavailable'
        }

        if (availableCountries.length === 0) {
            return selectedCodes.length > 0
                ? 'All available countries selected'
                : 'No countries available'
        }

        return 'Add a country'
    }

    return (
        <fieldset className="launch-country-filter">
            <legend>Launch country</legend>

            <div className="launch-country-control">
                <div className="launch-country-select">
                    <MapPin
                        aria-hidden="true"
                        size={17}
                    />

                    <select
                        value=""
                        disabled={
                            isPending ||
                            isError ||
                            availableCountries.length === 0
                        }
                        onChange={(event) =>
                            addCountry(event.target.value)
                        }
                        aria-label="Add launch country"
                    >
                        <option value="">
                            {placeholder()}
                        </option>

                        {availableCountries.map(
                            (country) => (
                                <option
                                    key={country.code}
                                    value={country.code}
                                >
                                    {country.name}
                                </option>
                            ),
                        )}
                    </select>
                </div>

                {selectedCountries.length > 0 && (
                    <div
                        className="selected-launch-countries"
                        aria-label="Selected launch countries"
                    >
                        {selectedCountries.map(
                            (country) => (
                                <span key={country.code}>
                                    {country.name}

                                    <button
                                        type="button"
                                        aria-label={`Remove ${country.name}`}
                                        onClick={() =>
                                            removeCountry(
                                                country.code,
                                            )
                                        }
                                    >
                                        <X
                                            aria-hidden="true"
                                            size={13}
                                        />
                                    </button>
                                </span>
                            ),
                        )}
                    </div>
                )}
            </div>
        </fieldset>
    )
}