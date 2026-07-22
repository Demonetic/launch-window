import type { PropsWithChildren, ReactNode } from 'react'
import { Link } from 'react-router'
import './auth.css'

interface AuthLayoutProps extends PropsWithChildren {
    title: string
    description: string
    footer: ReactNode
}

export function AuthLayout({
                               title,
                               description,
                               footer,
                               children,
                           }: AuthLayoutProps) {
    return (
        <main className="auth-page">
            <section className="auth-card">
                <Link className="auth-brand" to="/">
                    <span className="auth-brand-mark">LW</span>
                    <span>Launch Window</span>
                </Link>

                <header className="auth-header">
                    <p className="auth-eyebrow">
                        Mission control
                    </p>
                    <h1>{title}</h1>
                    <p>{description}</p>
                </header>

                {children}

                <footer className="auth-footer">
                    {footer}
                </footer>
            </section>
        </main>
    )
}