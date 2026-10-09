import { useState } from 'react'
import './App.css'
import Home, { CreationProcessPage, Historique, ListeAgent, ProcessPage, ProcessusDetailPageComponent, AgentDetailPageComponent, ProcessusEditor, ProcessusActifsPageComponent } from './page'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import PageLogin from './components/Login/Login'
import { Toaster } from './components/ui/sonner'


export const user = {
  nom: "Marcel Pecqueux",
  direction: "DSIUN",
  avatar: "MP",
  id: "02d86861-b9e7-45e9-903b-93e9efc7a304" // ID de test pour les notifications
};


function App() {
  const [open, setOpen] = useState(false)
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PageLogin />} />
        <Route path="/dashboard" element={<Home open={open} setOpen={setOpen} />} />
        <Route path="/liste" element={<ListeAgent open={open} setOpen={setOpen} />} />
        <Route path="/historique" element={<Historique open={open} setOpen={setOpen} />} />
        <Route path="/process_entree" element={<ProcessPage open={open} setOpen={setOpen} />} />
        <Route path="/process_sortie" element={<ProcessPage open={open} setOpen={setOpen} type="sortie" />} />
        <Route path="/creation_process/:templateId" element={<CreationProcessPage open={open} setOpen={setOpen} />} />
        <Route path="/processus/:processusId" element={<ProcessusDetailPageComponent open={open} setOpen={setOpen} />} />
        <Route path="/agent/:agentId" element={<AgentDetailPageComponent open={open} setOpen={setOpen} />} />
        <Route path="/processus-editor" element={<ProcessusEditor open={open} setOpen={setOpen} />} />
        <Route path="/processus-en-cours/:type"element={<ProcessusActifsPageComponent open={open} setOpen={setOpen} />}
        />

      </Routes>
      <Toaster closeButton />
    </BrowserRouter>
  )
}

export default App
