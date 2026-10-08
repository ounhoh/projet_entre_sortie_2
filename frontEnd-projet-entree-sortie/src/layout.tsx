import "./globals.css";

export default function RootLayout({
    Children,
}: Readonly<{
    Children: React.ReactNode
}>) {
    return <html lang="en">
    <body className="">
    {Children}
    </body></html>
}