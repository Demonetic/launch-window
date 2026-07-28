import { Rocket } from 'lucide-react'
import { useState } from 'react'

interface LaunchImageProps {
    src: string | null
    fallbackSize?: number
}

export function LaunchImage({
                                src,
                                fallbackSize = 38,
                            }: LaunchImageProps) {
    const [failedSource, setFailedSource] =
        useState<string | null>(null)

    const imageAvailable =
        src !== null && src !== failedSource

    if (!imageAvailable) {
        return (
            <Rocket
                aria-hidden="true"
                size={fallbackSize}
            />
        )
    }

    return (
        <img
            src={src}
            alt=""
            onError={() => setFailedSource(src)}
        />
    )
}